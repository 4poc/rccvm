from backend import BackendBase, registry

class Python2Backend(BackendBase):
    def compile(self, argv=None, env={}):
        pass

    def run(self, argv=None, env={}):
        if not argv: argv = ['python2', 'main.py']
        return self.popen(argv, env)

registry.append({
    'name': 'python2',
    'class': Python2Backend,
    'description': 'the general-purpose language'
})


