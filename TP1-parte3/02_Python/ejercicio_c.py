
import threading
import time
import random
import sys

DEFAULT_CAR = 15

# === Constantes de configuración ===
PARKING_CAPACITY = 6
PIT_COUNT = 3
SERVICE_CAPACITY = 2
WASH_CAPACITY = 1
HELPER_COUNT = 2

INSPECTION_TIME = 1.0
REPAIR_TIME = 2.0
OIL_CHANGE_TIME = 1.0
WASH_TIME = 1.0
ROLL_BACK_TIME = 1.0
OWNER_PICKUP_TIME = 1.0
ARRIVAL_TIME_MIN = 0.2
ARRIVAL_TIME_MAX = 0.5

# === Semáforos y mutex ===
sem_garage = threading.Semaphore(PARKING_CAPACITY + PIT_COUNT + SERVICE_CAPACITY + WASH_CAPACITY - 1)
sem_parking = threading.Semaphore(PARKING_CAPACITY)
sem_pits = threading.Semaphore(PIT_COUNT)
sem_service = threading.Semaphore(SERVICE_CAPACITY)
sem_wash = threading.Semaphore(WASH_CAPACITY)
sem_helpers = threading.Semaphore(HELPER_COUNT)

mutex_street_to_parking = threading.Lock()
mutex_parking_to_pit = threading.Lock()
mutex_pit_to_service = threading.Lock()
mutex_service_to_wash = threading.Lock()
mutex_print = threading.Lock()

def log(msg):
    with mutex_print:
        print(f"{msg}", flush=True)

def parking_area(name):
    # === Entrada al sistema ===
    log(f"{name} llega a la calle y espera turno para entrar al taller.")
    sem_garage.acquire()

    sem_parking.acquire()
    with mutex_street_to_parking:
        log(f"{name} entrado al estacionamiento.. (Richard lo mueve).")
    log(f"{name} entro al estacionamiento")

    # === Diagnóstico por Aaron ===
    time.sleep(INSPECTION_TIME)
    log(f"{name} es inspeccionado por Aaron.")

    pit_area(name)
    service_area(name)
    automatic_wash(name)

    # === Retiro del dueño ===
    with mutex_street_to_parking:
        time.sleep(OWNER_PICKUP_TIME)
    sem_parking.release()
    sem_garage.release()
    log(f"{name} es retirado del taller por su dueño.")

def pit_area(name):

    sem_pits.acquire()
    sem_helpers.acquire()
    with mutex_parking_to_pit:
        log(f"{name} entrando a la fosa...")
    sem_helpers.release()
    sem_parking.release()
    log(f"{name} entro en la fosa")
    # === Reparación ===
    time.sleep(REPAIR_TIME)
    log(f"{name} es reparado por Charles.")


def service_area(name):

    sem_service.acquire()
    sem_helpers.acquire()
    with mutex_pit_to_service:
        log(f"{name} entrando a zona de servicio...")
    sem_helpers.release()
    sem_pits.release()
    log(f"{name} entro a zona de servicio para cambio de aceite.")
    time.sleep(OIL_CHANGE_TIME)
    log(f"{name} termina el cambio de aceite.")


def automatic_wash(name):

    sem_wash.acquire()
    sem_helpers.acquire()
    with mutex_service_to_wash:
        log(f"{name} entrando a lavado automático...")
    sem_helpers.release()
    sem_service.release()
    log(f"{name} entro al lavado automático.")
    time.sleep(WASH_TIME)
    log(f"{name} fue lavado")
    sem_parking.acquire()
    time.sleep(ROLL_BACK_TIME)
    sem_wash.release()
    log(f"{name} fue llevado por los rodillos a la playa de estacionamiento")


def main():
    threads = []
    n = DEFAULT_CAR

    if len(sys.argv) > 1:
        try:
            n = int(sys.argv[1])
        except ValueError:
            print("Parámetro inválido, se usará valor por defecto.")

    for i in range(n):
        t = threading.Thread(target=parking_area, args=(f"Auto-{i+1}",))
        t.start()
        threads.append(t)
        time.sleep(random.uniform(ARRIVAL_TIME_MIN, ARRIVAL_TIME_MAX))

    for t in threads:
        t.join()

    log("\nSimulación finalizada. Todos los autos fueron atendidos.")

if __name__ == "__main__":
    main()
