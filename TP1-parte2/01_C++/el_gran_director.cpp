#include <algorithm>
#include <chrono>
#include <iomanip>
#include <iostream>
#include <random>
#include <string>
#include <thread>
#include <vector>

struct Team
{
  std::string name;
  int points = 0;
  int played = 0;
  int wins = 0;
  int draws = 0;
  int losses = 0;
  int goals_for = 0;
  int goals_against = 0;

  int GoalDifference() const 
{ 
  return goals_for - goals_against; 
}
};

struct Match
{
  int home_team;
  int away_team;
};

struct MatchResult
{
  int home_team;
  int away_team;
  int home_goals;
  int away_goals;
};

std::vector<Team> teams;
std::vector<std::vector<Match>> schedule;

void GenerateSchedule(int num_teams)
{
  schedule.clear();

  for (int round = 0; round < num_teams - 1; ++round)
  {
    std::vector<Match> matchday;

    for (int i = 0; i < num_teams / 2; ++i)
    {
      int home = (round + i) % (num_teams - 1);
      int away = (num_teams - 1 - i + round) % (num_teams - 1);
      if (i == 0)
      {
        away = num_teams - 1;
      }
      matchday.push_back(
      {
        home, away
      });
    }

    schedule.push_back(matchday);
  }
}

MatchResult SimulateMatch(const Match& match)
{
  std::random_device rd;
  std::mt19937 gen(rd());
  std::uniform_int_distribution<> goals(0, 5);
  std::uniform_real_distribution<> duration(100, 150);

  std::this_thread::sleep_for(
  std::chrono::milliseconds(static_cast<int>(duration(gen))));

  return 
  {
    match.home_team, match.away_team, goals(gen), goals(gen)
  };
}

void ShowTable(const std::vector<Team>& current_teams)
{
  std::cout << std::string(66, '-') << '\n';
  std::cout << std::left << std::setw(25) << "Equipo"
            << std::right << std::setw(5) << "Pts"
            << std::setw(5) << "PJ"
            << std::setw(5) << "PG"
            << std::setw(5) << "PE"
            << std::setw(5) << "PP"
            << std::setw(5) << "GF"
            << std::setw(5) << "GC"
            << std::setw(6) << "DIF" << '\n';
  std::cout << std::string(66, '-') << '\n';

  std::vector<Team> sorted = current_teams;
  std::sort(sorted.begin(), sorted.end(), [](const Team& a, const Team& b)
  {
    if (a.points != b.points) return a.points > b.points;
    return a.GoalDifference() > b.GoalDifference();
  });

  for (const auto& team : sorted)
  {
    std::cout << std::left << std::setw(25) << team.name
              << std::right << std::setw(5) << team.points
              << std::setw(5) << team.played
              << std::setw(5) << team.wins
              << std::setw(5) << team.draws
              << std::setw(5) << team.losses
              << std::setw(5) << team.goals_for
              << std::setw(5) << team.goals_against
              << std::setw(6) << team.GoalDifference() << '\n';
  }
}

void SimulateConcurrent()
{
  std::vector<Team> table = teams;
  auto start = std::chrono::high_resolution_clock::now();

  for (const auto& matchday : schedule)
  {
    std::vector<std::thread> threads;
    std::vector<MatchResult> results(matchday.size());

    for (size_t i = 0; i < matchday.size(); ++i)
    {
      threads.emplace_back([&results, &matchday, i]()
      {
        results[i] = SimulateMatch(matchday[i]);
      });
    }

    for (auto& thread : threads)
    {
      thread.join();
    }

    for (const auto& result : results)
    {
      Team& home = table[result.home_team];
      Team& away = table[result.away_team];

      home.goals_for += result.home_goals;
      home.goals_against += result.away_goals;
      away.goals_for += result.away_goals;
      away.goals_against += result.home_goals;

      home.played++;
      away.played++;

      if (result.home_goals > result.away_goals)
      {
        home.wins++;
        home.points += 3;
        away.losses++;
      } else if (result.home_goals < result.away_goals)
      {
        away.wins++;
        away.points += 3;
        home.losses++;
      } else
      {
        home.draws++;
        away.draws++;
        home.points++;
        away.points++;
      }
    }
  }

  auto end = std::chrono::high_resolution_clock::now();
  std::cout << "\nSimulación CONCURRENTE completada en: "
            << std::chrono::duration_cast<std::chrono::milliseconds>(end - start)
                   .count()
            << " ms\n";
  ShowTable(table);
}

void SimulateSequential()
{
  std::vector<Team> table = teams;
  auto start = std::chrono::high_resolution_clock::now();

  for (const auto& matchday : schedule)
  {
    for (const auto& match : matchday)
    {
      MatchResult result = SimulateMatch(match);

      Team& home = table[result.home_team];
      Team& away = table[result.away_team];

      home.goals_for += result.home_goals;
      home.goals_against += result.away_goals;
      away.goals_for += result.away_goals;
      away.goals_against += result.home_goals;

      home.played++;
      away.played++;

      if (result.home_goals > result.away_goals)
      {
        home.wins++;
        home.points += 3;
        away.losses++;
      } else if (result.home_goals < result.away_goals)
      {
        away.wins++;
        away.points += 3;
        home.losses++;
      } else
      {
        home.draws++;
        away.draws++;
        home.points++;
        away.points++;
      }
    }
  }

  auto end = std::chrono::high_resolution_clock::now();
  std::cout << "\nSimulación  SECUENCIAL completada en: "
            << std::chrono::duration_cast<std::chrono::milliseconds>(end - start)
                   .count()
            << " ms\n";
  ShowTable(table);
}

int main() 
{
  std::vector<std::string> team_names =
  {
      "River Plate",      "San Lorenzo",      "Ferro",
      "Huracan",          "Velez",            "Estudiantes(LP)",
      "Bolgrano",         "Lanus",            "Talleres(C)",
      "Dep. Espanol",     "San Martin(T)",    "Dep. Mandiyu(C)",
      "Rosario Central",  "Independiente",    "Racing Club",
      "Gimnasia(LP)",     "Platense",         "Argentinos",
      "Newells",          "Godoy Cruz"
   };

  for (const auto& name : team_names)
  {
    Team t;
    t.name = name;
    teams.push_back(t);
  }

  GenerateSchedule(static_cast<int>(teams.size()));

  std::cout << "=== Simulación CONCURRENTE ===\n";
  SimulateConcurrent();

  std::cout << "\n=== Simulación  SECUENCIAL ===\n";
  SimulateSequential();

  return 0;
}
