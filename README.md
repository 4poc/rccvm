# RCCVM - sandboxed code executioner

This project lets you execute untrusted source code in a safe environment. You submit code for compilation/execution through a json-based web interface (see api docs) that is then run inside a qemu virtual machine.

The vm is only exposed through a forwarded port (for the internal code runner) this bases its (network) security on the `restrict=y` feature for host-guest network isolation in qemu. It should be noted that qemu had [security issues](http://www.cvedetails.com/vulnerability-list/vendor_id-7506/Qemu.html) in the past that could potentially lead an attacker to gain access on the host.

The vm is running debian GNU/linux, the disk image is hosted [separately](http://apoc.cc/rccvm/).

Check the github wiki for a detailed api documentation.

## Example Task Request (Queue)

Here is a simple example that uses the task queue which asynchronously executes the code:

```
curl -s -d '{
  "backend": "python",
  "options": {},
  "files": {
    "main.py": {
      "data": "print 42"
    }
  }
}' http://127.0.0.1:9900/task | python -mjson.tool
```

This returns a task uuid instantly:

```
{
    "createdAt": "Dec 8, 2014 7:52:05 PM",
    "id": "6dc3ab8c-b2d6-44c5-809e-9b465d56f039",
    "startedAt": "Dec 8, 2014 7:52:05 PM",
    "terminated": false
}
```

The task can then be queried by `GET /task/6dc3ab8c-b2d6-44c5-809e-9b465d56f039`:

```
{
    "createdAt": "Dec 8, 2014 8:08:27 PM",
    "finishedAt": "Dec 8, 2014 8:08:27 PM",
    "id": "52dca261-5bc2-406e-bf1c-2209cac859a8",
    "response": {
        "run": {
            "code": 0,
            "stderr": "",
            "stdout": "42\n"
        }
    },
    "startedAt": "Dec 8, 2014 8:08:27 PM",
    "terminated": false
}
```

## Example Task Request

If the config option `bypass_task_queue` is true the vm is directly exposed, requests are directly submitted to the vm without any queuing.

```
curl -s -d '{
  "backend": "python",
  "options": {},
  "files": {
    "main.py": {
      "data": "print 42"
    }
  }
}' http://127.0.0.1:9900/execute | python -mjson.tool
```

Response:

```
{
        "run": {
            "code": 0,
            "stderr": "",
            "stdout": "42\n"
        }
}
```