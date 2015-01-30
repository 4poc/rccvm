from backend import BackendBase, registry

class ScalaBackend(BackendBase):
    def compile(self, argv=None, env={}):
        pass

    def run(self, argv=None, env={}):
        if not argv: argv = ['scala', 'main.sc']
        return self.popen(argv, env)

registry.append({
    'name': 'scala',
    'class': ScalaBackend,
    'description': 'the functional jvm language'
})


