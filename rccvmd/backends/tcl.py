from backend import BackendBase, registry

class TCLBackend(BackendBase):
    def compile(self, argv=None, env={}):
        pass

    def run(self, argv=None, env={}):
        if not argv: argv = ['tclsh', 'main.tcl']
        return self.popen(argv, env)

registry.append({
    'name': 'tcl',
    'class': TCLBackend,
    'description': 'the tool command language tcl.'
})


