from backend import BackendBase, registry

class CPPBackend(BackendBase):
    def compile(self, argv=None, env={}):
        if not argv: argv = ['g++', 'main.cpp']
        return self.popen(argv, env)

    def run(self, argv=None, env={}):
        if not argv: argv = ['./a.out']
        return self.popen(argv, env)

registry.append({
    'name': 'cpp',
    'class': CPPBackend,
    'description': 'The GNU C++ compiler'
})

