from backend import BackendBase, registry

class HaskellBackend(BackendBase):
    def compile(self, argv=None, env={}):
        if not argv: argv = ['ghc', 'main.hs']
        return self.popen(argv, env)

    def run(self, argv=None, env={}):
        if not argv: argv = ['./main']
        return self.popen(argv, env)

registry.append({
    'name': 'haskell',
    'class': HaskellBackend,
    'description': 'the purely-functional programming language'
})


