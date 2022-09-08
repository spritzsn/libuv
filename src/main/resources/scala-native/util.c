#include <uv.h>

int libuv_eof() { return UV_EOF; }
unsigned int libuv_sockaddr_in_size() { return sizeof(struct sockaddr_in); }
unsigned int libuv_sockaddr_storage_size() { return sizeof(struct sockaddr_storage); }
struct addrinfo *libuv_get_hints(uv_getaddrinfo_t *req) { return req->hints; }
struct addrinfo *libuv_get_addrinfo(uv_getaddrinfo_t *req) { return req->addrinfo; }
