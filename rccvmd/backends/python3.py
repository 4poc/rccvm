from backend import BackendBase, registry

class Python3Backend(BackendBase):
    def compile(self, argv=None, env={}):
        pass

    def run(self, argv=None, env={}):
        if not argv: argv = ['python3', 'main.py']
        return self.popen(argv, env)

registry.append({
    'name': 'python3',
    'class': Python3Backend,
    'description': 'the general-purpose language'
})


