from backend import BackendBase, registry

class BashBackend(BackendBase):
    def compile(self, argv=None, env={}):
        pass

    def run(self, argv=None, env={}):
        if not argv: argv = ['/bin/bash', 'main.sh']
        return self.popen(argv, env)

registry.append({
    'name': 'bash',
    'class': BashBackend,
    'description': 'the bash shell'
})


