#include <uv.h>

int uv_eof() { return UV_EOF; }
unsigned int uv_sockaddr_in_size() { return sizeof(struct sockaddr_in); }
unsigned int uv_sockaddr_storage_size() { return sizeof(struct sockaddr_storage); }
