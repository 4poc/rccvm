import re
import os
import os.path
from os.path import realpath, join, dirname, abspath
from subprocess import Popen, PIPE
import importlib
from files import RequestFiles

registry = []
def load():
    for file in os.listdir(realpath(join(\
            dirname(abspath(__file__)), './backends'))):
        m = re.match(r'^([^\_].*)\.py', file)
        if m: importlib.import_module('backends.'+m.group(1)) 

class BackendBase(object):
    """Backend runner that compiles and executes code."""
    def __init__(self, files, options):
        self.files = files
        self.options = options

    # returns popen instance
    def compile(self, argv=None, env={}):
        pass

    # returns popen instance
    def run(self, argv=None, env={}):
        pass

    def popen(self, argv, env):
        return Popen(argv,
                stdin=PIPE, stdout=PIPE, stderr=PIPE,
                cwd=self.files.path,
                env=self.getMergedEnv(env))

    # Merge the environment with the supplied one and return
    def getMergedEnv(self, env):
        current = dict(os.environ)
        current.update(env)
        return current

class Runner(object):
    def __init__(self):
        self.backends = {}
        for backend in registry:
            self.backends[backend['name']] = backend

    def delegate(self, request):
        backend = request.get('backend', 'bash')
        if not backend in self.backends:
            raise 'specified backend not found'

        result = {}

        backendClass = self.backends[backend]['class']
        files = RequestFiles(request['files'])
        # backend options
        options = request.get('options', {})

        instance = backendClass(files, options)

        compile = {
                'env': {},
                'argv': None
                }
        run = compile.copy()
        compile.update(request.get('compile', {}))
        run.update(request.get('run', {}))

        # write submitted files locally in a temporary directory:
        files.store()

        try:
            process = instance.compile(**compile)
            if process:
                process.wait()
                result['compile'] = {
                        'stdout': process.stdout.read(),
                        'stderr': process.stderr.read(),
                        'code': process.returncode
                        }

            process = instance.run(**run)
            process.wait()
            result['run'] = {
                    'stdout': process.stdout.read(),
                    'stderr': process.stderr.read(),
                    'code': process.returncode
                    }

        except Exception,e:
            result['error'] = {
                    'type': type(e).__name__,
                    'message': str(e)
                    }
        finally:
            # cleanup
            files.destroy()

        return result

