# RCCVM - sandboxed code executioner

![rccvm overview](http://apoc.cc/rccvm_overview.png)

Runner for user submitted (untrusted) source code. Clients submit code through a [HTTP interface](https://github.com/4poc/rccvm/wiki) the code is then compiled/run in an isolated qemu virtual machine and sent back to the client.

Within the vm runs a code dispatching server that is exposed through a forwarded port, this uses the user networking feature of qemu and network access is restricted to this one port. It should be noted that qemu had [security issues](http://www.cvedetails.com/vulnerability-list/vendor_id-7506/Qemu.html) in the past that could potentially lead an attacker to gain access on the host.

The vm is running Debian GNU/Linux, the disk image is hosted [separately](http://apoc.cc/rccvm/).

## Try It

To try it out you can query the bot 'rccvm' on either Freenode or Quakenet IRC:

```
<you> /msg rccvm help rccvm
<rccvm> execute code: (lang)> (code) | languages: python2 (py), python3 (py3), scala (sc), tcl (tcl), ruby (rb), perl (pl), haskell (hs), clojure (clj), php (php), bash (sh), cpp (c++), gcc (c), java (java)
<you> c> printf("Hi IRC!");
<rccvm> [output] Hi IRC!
```

Contact me if you want rccvm to join your channel :)

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
