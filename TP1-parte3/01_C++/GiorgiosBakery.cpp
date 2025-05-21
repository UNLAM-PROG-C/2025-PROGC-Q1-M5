#include <iostream>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <queue>
#include <vector>
#include <random>
#include <chrono>

using namespace std;

// Constants
const int TABLE_CAPACITY = 20;
const int BASKET_CAPACITY = 50;
const int COUNTER_CAPACITY = 30;

const int MASTER_DOUGH_AMOUNT = 2;
const int ASSISTANT_DOUGH_AMOUNT = 1;
const int BAKER_BATCH_SIZE = 5;
const int PACKER_BAG_SIZE = 3;

const int MASTER_WORK_TIME_MS = 100;
const int ASSISTANT_WORK_TIME_MS = 120;
const int BAKER_WORK_TIME_MS = 200;
const int PACKER_WORK_TIME_MS = 100;
const int PACKER_SCALE_TIME_MS = 100;
const int SELLER_SERVICE_TIME_MS = 150;

const int TOTAL_CLIENTS = 3;
const int MAX_BAGS_PER_CLIENT = 3;

mutex mtx_table, mtx_basket, mtx_counter, mtx_scale, mtx_sales;
condition_variable cv_table_full, cv_table_empty;
condition_variable cv_basket_full, cv_basket_empty;
condition_variable cv_counter_full;
condition_variable cv_new_client, cv_client_served;

struct ClientOrder 
{
    int id;
    int desired_quantity;
    bool served = false;
};

queue<ClientOrder*> client_queue;
vector<thread> client_threads;

int dough_table = 0;
int baked_basket = 0;
int counter_bags = 0;
int bags_sold = 0;
int clients_served = 0;

bool production_finished = false;

void work(int milliseconds) 
{
    this_thread::sleep_for(chrono::milliseconds(milliseconds));
}

// Step 1
void master_baker() 
{
    while (true) 
    {
        work(MASTER_WORK_TIME_MS);
        unique_lock<mutex> lock(mtx_table);
        if (production_finished) break;
        cv_table_full.wait(lock, [] 
        {
            return (dough_table + MASTER_DOUGH_AMOUNT <= TABLE_CAPACITY) || production_finished;
        });
        if (production_finished) break;
        dough_table += MASTER_DOUGH_AMOUNT;
        cout << "Maestro panadero hizo " << MASTER_DOUGH_AMOUNT << " bollos. Bollos en la mesa: " << dough_table << endl;
        lock.unlock();
        cv_table_empty.notify_all();
    }
}

void assistant_baker() 
{
    while (true) 
    {
        work(ASSISTANT_WORK_TIME_MS);
        unique_lock<mutex> lock(mtx_table);
        if (production_finished) break;
        cv_table_full.wait(lock, [] 
        {
            return (dough_table + ASSISTANT_DOUGH_AMOUNT <= TABLE_CAPACITY) || production_finished;
        });
        if (production_finished) break;
        dough_table += ASSISTANT_DOUGH_AMOUNT;
        cout << "Ayudante hizo " << ASSISTANT_DOUGH_AMOUNT << " bollo. Bollos en la mesa: " << dough_table << endl;
        lock.unlock();
        cv_table_empty.notify_all();
    }
}

// Step 2
void baker() 
{
    while (true) 
    {
        unique_lock<mutex> lock(mtx_table);
        if (production_finished && dough_table < BAKER_BATCH_SIZE) break;
        cv_table_empty.wait(lock, [] 
        {
            return dough_table >= BAKER_BATCH_SIZE || production_finished;
        });
        if (production_finished && dough_table < BAKER_BATCH_SIZE) break;
        dough_table -= BAKER_BATCH_SIZE;
        cout << "Cocinero tomó " << BAKER_BATCH_SIZE << " bollos. Ahora hay en mesa: " << dough_table << endl;
        lock.unlock();
        cv_table_full.notify_all();

        work(BAKER_WORK_TIME_MS);

        unique_lock<mutex> lock_basket(mtx_basket);
        cv_basket_full.wait(lock_basket, [] 
        {
            return (baked_basket + BAKER_BATCH_SIZE <= BASKET_CAPACITY) || production_finished;
        });
        if (production_finished && (baked_basket + BAKER_BATCH_SIZE > BASKET_CAPACITY)) break;
        baked_basket += BAKER_BATCH_SIZE;
        cout << "Horno horneó " << BAKER_BATCH_SIZE << " panes. Canasta tiene: " << baked_basket << endl;
        lock_basket.unlock();
        cv_basket_empty.notify_all();
    }
}

// Step 3
void packer(int id) 
{
    while (true) 
    {
        unique_lock<mutex> lock_basket(mtx_basket);
        if (production_finished && baked_basket < PACKER_BAG_SIZE) break;
        cv_basket_empty.wait(lock_basket, [] 
        {
            return baked_basket >= PACKER_BAG_SIZE || production_finished;
        });
        if (production_finished && baked_basket < PACKER_BAG_SIZE) break;
        baked_basket -= PACKER_BAG_SIZE;
        cout << "Empaquetador " << id << " tomó " << PACKER_BAG_SIZE << " panes. Canasta con: " << baked_basket << endl;
        lock_basket.unlock();
        cv_basket_full.notify_all();

        work(PACKER_WORK_TIME_MS);
        cout << "Empaquetador " << id << " está etiquetando." << endl;

        
        {
            unique_lock<mutex> scale_lock(mtx_scale);
            work(PACKER_SCALE_TIME_MS);
            cout << "Empaquetador " << id << " está pesando." << endl;
        }

        unique_lock<mutex> lock_counter(mtx_counter);
        if (counter_bags < COUNTER_CAPACITY) 
        {
            counter_bags += 1;
            cout << "Empaquetador " << id << " puso 1 bolsita. Mostrador con: " << counter_bags << endl;
            cv_counter_full.notify_all();
        }
        else if (production_finished) 
        {
            break;
        }
    }
}

void seller() 
{
    while (true) 
    {
        ClientOrder* order = nullptr;
        unique_lock<mutex> lock_sales(mtx_sales);
        cv_new_client.wait(lock_sales, [] 
        {
            return !client_queue.empty() || production_finished;
        });
        if (client_queue.empty() && production_finished) break;

        order = client_queue.front();

        unique_lock<mutex> lock_counter(mtx_counter);
        cv_counter_full.wait(lock_counter, [&order] 
        {
            return counter_bags >= order->desired_quantity || production_finished;
        });

        if (counter_bags < order->desired_quantity && production_finished) 
        {
            client_queue.pop();
            delete order;
            continue;
        }

        counter_bags -= order->desired_quantity;
        bags_sold += order->desired_quantity;
        cout << "Vendedora atendió al cliente " << order->id
             << ". Vendió " << order->desired_quantity
             << " bolsita(s). Mostrador hay: " << counter_bags << endl;

        client_queue.pop();
        order->served = true;
        clients_served++;

        lock_counter.unlock();
        cv_client_served.notify_all();

        this_thread::sleep_for(chrono::milliseconds(SELLER_SERVICE_TIME_MS));
    }

    cout << "\n=== Ventas finalizadas. Total bolsitas vendidas: "
         << bags_sold << " ===\n";
}

// Clients
void client(int id, int quantity) 
{
    ClientOrder* order = new ClientOrder
    {id, quantity};
    
    {
        unique_lock<mutex> lock(mtx_sales);
        client_queue.push(order);
        cout << "Cliente " << id << " quiere comprar " << quantity << " bolsita(s)." << endl;
    }

    cv_new_client.notify_one();

    
    {
        unique_lock<mutex> lock(mtx_sales);
        cv_client_served.wait(lock, [&order] 
        { return order->served; });
    }

    delete order;
}

void client_generator() 
{
    random_device rd;
    mt19937 gen(rd());
    uniform_int_distribution<> dist(1, MAX_BAGS_PER_CLIENT);

    for (int i = 1; i <= TOTAL_CLIENTS; ++i) 
    {
        int quantity = dist(gen);
        client_threads.emplace_back(client, i, quantity);
        this_thread::sleep_for(chrono::milliseconds(100));
    }
}

int main() 
{
    thread t_master(master_baker);
    thread t_assistant(assistant_baker);
    thread t_baker(baker);
    thread t_packer1(packer, 1);
    thread t_packer2(packer, 2);
    thread t_seller(seller);
    thread t_generator(client_generator);

    t_generator.join();
    for (auto& t : client_threads) t.join();

    // PProduction finished
    
    {
        unique_lock<mutex> lock(mtx_sales);
        production_finished = true;
    }

    // Notify All
    cv_new_client.notify_all();
    cv_counter_full.notify_all();
    cv_basket_empty.notify_all();
    cv_basket_full.notify_all();
    cv_table_empty.notify_all();
    cv_table_full.notify_all();

    t_seller.join();
    t_master.join();
    t_assistant.join();
    t_baker.join();
    t_packer1.join();
    t_packer2.join();

    return 0;
}
