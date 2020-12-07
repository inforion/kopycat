#include <stdint.h>
#include <stdio.h>

typedef uint8_t u8;
typedef uint32_t u32;

//#define MEM_DMA_BUF  (volatile u8*) 0x80100000
//#define REG_UART_OUT (volatile u32*) 0xB0001000


int receive(u8* buf, int size) {
    int i;
    for (i = 0; i < size; ++i)
        buf[i] = 0;
    return size;
}

void uart_send_char(u32 x) {
    int y = x;
}


int cmd_0xAA(u8* data) {
	int k = 0;
	int len = data[0];
	u8 buf[16] = { 0 };
	for (k = 0; k < len; k++) {
		buf[k] = data[k + 1];
	}

	for (k = 0; k < len; k++) {
        uart_send_char(buf[k]);
//		*REG_UART_OUT = buf[k];
	}
	return len;
}

int cmd_0xBB(u8* data) {
	int k = 0;
	int len = data[0];

	for (k = 0; k < len; k++) {
		if (data[k + 1] == 0xDD)  {
			return 1;
		} else if (data[k + 1] == 0xEE) {
			return 2;
		} else if (data[k + 1] == 0xFF) {
			return 3;
		}
	}

	return -1;
}

int cmd_0xCC(u8* data) {
	return -1;
}


int parse_body(u8* body) {
	u8 cmd = body[0];
	switch (cmd) {
		case 0xAA:
			return cmd_0xAA(body + 1);
		case 0xBB:
			return cmd_0xBB(body + 1);
		case 0xCC:
			return cmd_0xCC(body + 1);
		default:
			return -1;
	}
}

int parse_packet() {
    u8 buf[256];
    
    int count = receive(buf, 256);
    
    if (count <= 0)
        return -1;
    
	if (buf[0] == 0xC0 && buf[1] == 0xBA) {
		return parse_body(buf + 2);
	}

	return 0;
}

int main() {
    printf("Starting parsing packet!!!");
	return parse_packet();
}
