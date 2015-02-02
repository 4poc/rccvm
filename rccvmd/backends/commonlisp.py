from backend import BackendBase, registry

class CommonLispBackend(BackendBase):
    def compile(self, argv=None, env={}):
        pass

    def run(self, argv=None, env={}):
        if not argv: argv = ['gcl', '-batch', '-load', 'main.lisp']
        return self.popen(argv, env)

registry.append({
    'name': 'commonlisp',
    'class': CommonLispBackend,
    'description': 'the modern lisp'
})


