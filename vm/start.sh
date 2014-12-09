#!/bin/sh

# starts the vm for testing/development

IMAGE="rccvm-disk.qcow2"

qemu-system-x86_64 \
    -boot menu=on \
    -m 128 \
    -enable-kvm \
    -net nic -net user,hostfwd=tcp:127.0.0.1:8654-10.0.2.15:5000 \
    -monitor tcp:127.0.0.1:9007,server,nowait \
    -vnc :5 \
    $IMAGE

