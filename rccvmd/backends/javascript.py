from backend import BackendBase, registry

class JavaScriptBackend(BackendBase):
    def compile(self, argv=None, env={}):
        pass

    def run(self, argv=None, env={}):
        if not argv: argv = ['node', 'main.js']
        return self.popen(argv, env)

registry.append({
    'name': 'javascript',
    'class': JavaScriptBackend,
    'description': 'nodejs'
})


