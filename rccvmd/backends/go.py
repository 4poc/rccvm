from backend import BackendBase, registry

class GoBackend(BackendBase):
    def compile(self, argv=None, env={}):
        pass

    def run(self, argv=None, env={}):
        if not argv: argv = ['go', 'run', 'main.go']
        return self.popen(argv, env)

registry.append({
    'name': 'go',
    'class': GoBackend,
    'description': 'go'
})


