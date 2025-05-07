import os
import random
import time

PLAYER = 5
THROWS = 10

def player(id):
    print(f"Jugador {id} entra al juego.", flush=True)
    points = 0
    for i in range(THROWS):
        dice = random.randint(1, 6)
        points += dice
        print(f"Jugador {id} - Lanzamiento {i+1}: {dice}", flush=True)
        time.sleep(random.uniform(0.1, 0.3))
    print(f"Jugador {id} finaliza con {points} puntos.", flush=True)

def main():
    children = []
    for i in range(PLAYER):
        pid = os.fork()
        if pid == 0:
            player(i + 1)
            os._exit(0) 
        else:
            children.append(pid)

    for pid in children:
        os.waitpid(pid, 0)

    print("Todos los jugadores han terminado.")

if __name__ == "__main__":
    main()
    time.sleep(10)
