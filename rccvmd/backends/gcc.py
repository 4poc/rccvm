from backend import BackendBase, registry

class GCCBackend(BackendBase):
    def compile(self, argv=None, env={}):
        if not argv: argv = ['gcc', 'main.c']
        return self.popen(argv, env)

    def run(self, argv=None, env={}):
        if not argv: argv = ['./a.out']
        return self.popen(argv, env)

registry.append({
    'name': 'gcc',
    'class': GCCBackend,
    'description': 'the GNU c/c++ compiler'
})

