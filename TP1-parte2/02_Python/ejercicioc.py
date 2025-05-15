import csv
import json
import threading
from collections import Counter

TOP_RESULT = 1

results = []

def process_user(user_id, rows):
    genres = []
    types = []

    for row in rows:
        genres.append(row["genre"])
        types.append(row["type"])

    genre_counter = Counter(genres)
    type_counter = Counter(types)

    preferred_genre = genre_counter.most_common(TOP_RESULT)[0][0]
    preferred_type = type_counter.most_common(TOP_RESULT)[0][0]

    unique_genres = set(genres)

    result = {
        "user_id": int(user_id),
        "user_name": rows[0]["user_name"],
        "chosen_genre": preferred_genre,
        "chosen_type": preferred_type,
        "total": len(rows),
        "different_genres": len(unique_genres)
    }

    results.append(result)

def read_file(path):
    users = {}

    with open(path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)

        for row in reader:
            user_id = row["user_id"]
            key = user_id

            if key not in users:
                users[key] = []

            users[key].append(row)

    return users

def main():
    users = read_file("visualizaciones.csv")
    threads = []

    for user_id, rows_id in users.items():
        thread = threading.Thread(target=process_user, args=(user_id, rows_id))
        thread.start()
        threads.append(thread)

    for thread in threads:
        thread.join()

    with open("preferencias.json", "w", encoding="utf-8") as jsonfile:
        json.dump(results, jsonfile, indent=2, ensure_ascii=False)

    print("Archivo preferencias.json generado con éxito.")

if __name__ == "__main__":
    main()