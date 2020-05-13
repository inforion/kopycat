//
// Created by Alexei Gladkikh on 05.05.2020.
//

#ifndef USART_CRC_STREAM_H
#define USART_CRC_STREAM_H

#include <stdint.h>

typedef struct STREAM stream_t;

typedef struct {
    void (*destroy) (stream_t* stream);
    void (*add) (stream_t* stream, uint8_t byte);
    uint8_t (*get_byte) (stream_t* stream, int index);
    int (*get_size) (stream_t* stream);
    int (*get_pos) (stream_t* stream);
    void (*set_pos) (stream_t* stream, int pos);
    void (*reset) (stream_t* stream);
    void (*shrink) (stream_t* stream);
    int (*to_buffer) (stream_t* stream, uint8_t* buffer, int size, int start, int offset);
} stream_ops_t;

struct STREAM {
    const stream_ops_t* ops;
    uint8_t *buf_ptr;
    int buf_size;
    int size;
    int pos;
};


stream_t* new_stream();

#endif //USART_CRC_STREAM_H
