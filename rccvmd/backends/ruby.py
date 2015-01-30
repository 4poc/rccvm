from backend import BackendBase, registry

class RubyBackend(BackendBase):
    def compile(self, argv=None, env={}):
        pass

    def run(self, argv=None, env={}):
        if not argv: argv = ['ruby', 'main.rb']
        return self.popen(argv, env)

registry.append({
    'name': 'ruby',
    'class': RubyBackend,
    'description': 'the dynamic scripting language'
})


