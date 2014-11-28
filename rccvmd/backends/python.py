from backend import BackendBase, registry

class PythonBackend(BackendBase):
    def compile(self, argv=None, env={}):
        pass

    def run(self, argv=None, env={}):
        if not argv: argv = ['python', 'main.py']
        return self.popen(argv, env)

registry.append({
    'name': 'python',
    'class': PythonBackend,
    'description': 'the general-purpose language python.'
})


