from backend import BackendBase, registry

class ClojureBackend(BackendBase):
    def compile(self, argv=None, env={}):
        pass

    def run(self, argv=None, env={}):
        if not argv: argv = ['clojure', 'main.clj']
        return self.popen(argv, env)

registry.append({
    'name': 'clojure',
    'class': ClojureBackend,
    'description': 'the jvm lisp'
})


