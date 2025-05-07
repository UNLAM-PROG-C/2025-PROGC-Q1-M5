#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/wait.h>

#define PROCESS_A 'A'
#define PROCESS_B 'B'
#define PROCESS_C 'C'
#define PROCESS_D 'D'
#define PROCESS_E 'E'
#define PROCESS_F 'F'
#define PROCESS_G 'G'
#define PROCESS_H 'H'
#define PROCESS_I 'I'

#define CHILD_PROCESS 0
#define WAIT_TIME 5

void print_process_info(char name)
{
    printf("Soy el proceso %c - PID: %d, mi padre es PID: %d\n", name, getpid(), getppid());
    fflush(stdout);
}

int main(int argc, char *argv[])
{
    pid_t pid_B = fork();

    if( pid_B > CHILD_PROCESS )
    {
        print_process_info(PROCESS_A);
        sleep(WAIT_TIME);
        wait(NULL);
    }
    else
    {
        print_process_info(PROCESS_B);

        pid_t pid_C = fork();
        if( pid_C == CHILD_PROCESS )
        {
            print_process_info(PROCESS_C);

            pid_t pid_E = fork();
            if( pid_E == CHILD_PROCESS )
            {
                print_process_info(PROCESS_E);

                pid_t pid_H = fork();
                if( pid_H == CHILD_PROCESS )
                {
                    print_process_info(PROCESS_H);
                    sleep(WAIT_TIME);
                    exit(EXIT_SUCCESS);
                }

                pid_t pid_I = fork();
                if( pid_I == CHILD_PROCESS )
                {
                    print_process_info(PROCESS_I);
                    sleep(WAIT_TIME);
                    exit(EXIT_SUCCESS);
                }

                sleep(WAIT_TIME);
                exit(EXIT_SUCCESS);
            }

            sleep(WAIT_TIME);
            exit(EXIT_SUCCESS);
        }

        pid_t pid_D = fork();
        if( pid_D == CHILD_PROCESS )
        {
            print_process_info(PROCESS_D);

            pid_t pid_F = fork();
            if( pid_F == CHILD_PROCESS )
            {
                print_process_info(PROCESS_F);
                sleep(WAIT_TIME);
                exit(EXIT_SUCCESS);
            }

            pid_t pid_G = fork();
            if( pid_G == CHILD_PROCESS )
            {
                print_process_info(PROCESS_G);
                sleep(WAIT_TIME);
                exit(EXIT_SUCCESS);
            }

            sleep(WAIT_TIME);
            exit(EXIT_SUCCESS);
        }

        sleep(WAIT_TIME);
        return EXIT_SUCCESS;
    }

    return EXIT_SUCCESS;
}
