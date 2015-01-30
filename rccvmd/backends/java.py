from backend import BackendBase, registry

class JavaBackend(BackendBase):
    def compile(self, argv=None, env={}):
        if not argv: argv = ['javac', 'Main.java']
        return self.popen(argv, env)

    def run(self, argv=None, env={}):
        if not argv: argv = ['java', 'Main']
        return self.popen(argv, env)

registry.append({
    'name': 'java',
    'class': JavaBackend,
    'description': 'the cross-platform language'
})


