from backend import BackendBase, registry

class PHPBackend(BackendBase):
    def compile(self, argv=None, env={}):
        pass

    def run(self, argv=None, env={}):
        if not argv: argv = ['php', 'main.php']
        return self.popen(argv, env)

registry.append({
    'name': 'php',
    'class': PHPBackend,
    'description': 'personal homepage language'
})


