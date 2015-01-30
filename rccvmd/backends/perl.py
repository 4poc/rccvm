from backend import BackendBase, registry

class PerlBackend(BackendBase):
    def compile(self, argv=None, env={}):
        pass

    def run(self, argv=None, env={}):
        if not argv: argv = ['perl', 'main.pl']
        return self.popen(argv, env)

registry.append({
    'name': 'perl',
    'class': PerlBackend,
    'description': 'the general-purpose dynamic programming language'
})


