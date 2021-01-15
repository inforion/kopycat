#include <stdio.h>
#include <pthread.h>

pthread_t threads[10];
int result[10];

void* thread_main(void* data) {
	int ind = (int)data;
	printf("Thread index: %d\n", ind);
	return (void*)(long)ind;
}

int main() {
	int i;
	for (i = 0; i < 10; ++i) {
		pthread_create(threads + i, NULL, thread_main, (void*)(long)i);	
		result[i] = 0;
	}
	
	for (i = 0; i < 10; ++i) {
		int data;
		pthread_join(threads[i], (void**)&data);
		printf("Joined thread: %d\n", data);
		result[data] = 1;
	}
}
