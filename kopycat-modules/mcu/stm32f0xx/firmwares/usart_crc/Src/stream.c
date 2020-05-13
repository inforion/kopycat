//
// Created by Alexei Gladkikh on 05.05.2020.
//

#include "stream.h"
#include <stdlib.h>
#include <string.h>
#include <assert.h>

void destroy(stream_t* stream) {
    free(stream->buf_ptr);
    stream->buf_size = 0;
    stream->pos = 0;
    stream->size = 0;
    stream->ops = 0;
    free(stream);
}

void add(stream_t* stream, uint8_t byte) {
    if (stream->pos == stream->size) {
        if (stream->size == stream->buf_size) {
            stream->buf_ptr = realloc(stream->buf_ptr, stream->buf_size * 2);
            assert(stream->buf_ptr != NULL);
        }
        stream->size++;
    }
    stream->buf_ptr[stream->pos] = byte;
    stream->pos++;
}

uint8_t get_byte(stream_t* stream, int index) {
    assert(index < stream->size);
    return stream->buf_ptr[index];
}

int get_pos(stream_t* stream) {
    return stream->pos;
}

void set_pos(stream_t* stream, int pos) {
    assert(pos < stream->size);
    stream->pos = pos;
}

int get_size(stream_t* stream) {
    return stream->size;
}

void reset(stream_t* stream) {
    stream->size = 0;
    stream->pos = 0;
}

void shrink(stream_t* stream) {
    if (stream->size != stream->buf_size) {
        // TODO: BUG HERE IF SIZE == 0
        stream->buf_ptr = realloc(stream->buf_ptr, stream->size);
        assert(stream->buf_ptr != NULL);
    }
}

int to_buffer(stream_t* stream, uint8_t* buffer, int size, int start, int offset) {
    assert(offset < size);
    assert(start < stream->size);
    // TODO: BUG HERE
    size_t copy = (size < stream->size) ? size : stream->size;
    memcpy(buffer + offset, stream->buf_ptr + start, copy);
    return copy;
}

const stream_ops_t sops = {
        .destroy = destroy,
        .add = add,
        .get_byte = get_byte,
        .get_size = get_size,
        .get_pos = get_pos,
        .set_pos = set_pos,
        .reset = reset,
        .shrink = shrink,
        .to_buffer = to_buffer
};

stream_t* new_stream() {
    stream_t *s = malloc(sizeof(stream_t));
    s->ops = &sops;
    s->buf_size = 8;
    s->buf_ptr = malloc(s->buf_size);
    assert(s->buf_ptr != NULL);
    s->size = 0;
    s->pos = 0;
    return s;
}

