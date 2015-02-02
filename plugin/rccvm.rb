
require 'mechanize'

class RccvmPlugin < Plugin
  BackendMap = [
    {
      :backend => 'python2',
      :aliases => %w{py python python2},
      :filename => 'main.py',
      :content => '%s'
    },
    {
      :backend => 'python3',
      :aliases => %w{py3 python3},
      :filename => 'main.py',
      :content => '%s'
    },
    {
      :backend => 'scala',
      :aliases => %w{sc scala},
      :filename => 'main.sc',
      :content => '%s'
    },
    {
      :backend => 'tcl',
      :aliases => %w{tcl},
      :filename => 'main.tcl',
      :content => '%s'
    },
    {
      :backend => 'ruby',
      :aliases => %w{rb ruby},
      :filename => 'main.rb',
      :content => '%s'
    },
    {
      :backend => 'perl',
      :aliases => %w{pl perl},
      :filename => 'main.pl',
      :content => '%s'
    },
    {
      :backend => 'haskell',
      :aliases => %w{hs haskell},
      :filename => 'main.hs',
      :content => '%s'
    },
    {
      :backend => 'clojure',
      :aliases => %w{clj clojure},
      :filename => 'main.clj',
      :content => '%s'
    },
    {
      :backend => 'php',
      :aliases => %w{php},
      :filename => 'main.php',
      :content => '<?php %s'
    },
    {
      :backend => 'bash',
      :aliases => %w{sh bash},
      :filename => 'main.sh',
      :content => '%s'
    },
    {
      :backend => 'cpp',
      :aliases => %w{c++ cpp},
      :filename => 'main.cpp',
      :content => <<-eos
          #include<iostream>
          #include<string>
          #include<stdlib.h>
          using namespace std;
          int main(void) {
            %s
            return 0;
          }
eos
    },
    {
      :backend => 'gcc',
      :aliases => %w{c gcc},
      :filename => 'main.c',
      :content => <<-eos
          #include<stdio.h>
          #include<stdlib.h>
          int main(int argc, char** argv) {
            %s
            return 0;
          }
eos
    },
    {
      :backend => 'java',
      :aliases => %w{java},
      :filename => 'Main.java',
      :content => <<-eos
          class Main {
            public static void main(String[] args) {
              %s
            }
          }
eos
    },
    {
      :backend => 'javascript',
      :aliases => %w{js javascript},
      :filename => 'main.js',
      :content => '%s'
    },
    {
      :backend => 'go',
      :aliases => %w{go},
      :filename => 'main.go',
      :content => <<-eos
    package main
    import "fmt"; func main() { %s }
eos
    },
    {
      :backend => 'commonlisp',
      :aliases => %w{cl lisp},
      :filename => 'main.lisp',
      :content => '%s'
    },
  ]
  OutputLabel = Color.to_s + ColorCode[:teal].to_s + '[output] ' + NormalText
  ErrorLabel = Color.to_s + ColorCode[:red].to_s + '[error] ' + NormalText 

  Config.register(Config::StringValue.new('rccvm.url',
    :default => nil,
    :desc => 'rccvm url'))

  def initialize
    super
    @backends = {}
    BackendMap.each { |backend|
      backend[:aliases].each { |a| @backends[a] = backend }
    }
    debug 'loaded backend aliases: ' + @backends.keys.join('|')
    @agent = Mechanize.new
  end

  def help(plugin, topic='')
    langs = BackendMap.map{|b|b[:backend] + ' ('+b[:aliases].first+')'}.join(', ')
    'execute code: '+Bold+'(lang)> (code)'+NormalText+' | languages: ' + langs
  end

  def run_code(m, name, code, backend)
    from = (m.channel || m.source).to_s
    resp = execute(backend, code)
    content = filter(get_content(resp), m.channel)
    if content.length > 0
      m.reply OutputLabel + content
    end
  end

  def run_continue(m, name, code)
    from = (m.channel || m.source).to_s
  end

  def message(m, dummy=nil)
    if m.message.match %r{^([^\> ]+)\>(.*)$}
      name = $1.strip
      code = $2.strip
      backend = @backends.has_key?(name) ? @backends[name] : nil

      debug '[lang:%s code:%s backend:%s]' % [name.inspect, code.inspect, backend.inspect]

      Thread.new do
        begin
          if backend
            run_code(m, name, code, backend)
          elsif name == '='
            run_continue(m, name, code) 
          end
        rescue
          error = $!.to_s
          debug 'error occured: ' + error
          debug $@.join("\n")
          m.reply ErrorLabel + filter(error, m.channel)
        end
      end
    end
  end

  private

  def execute(backend, code)
    url = @bot.config['rccvm.url']
    req = {
      'backend' => backend[:backend],
      'files' => {
        backend[:filename] => {
          'data' => backend[:content] % [code]
        }
      }
    }
    debug 'send request: ' + req.inspect
    res = @agent.post(url + '/task', JSON.generate(req))
    debug 'response: ' + res.body

    taskId = JSON.parse(res.body)['id']

    resp = nil
    # wait for the task to finish:
    while true
      res = @agent.get(url + '/task/' + taskId)
      taskInfo = JSON.parse(res.body)
      if taskInfo.has_key? 'response'
        resp = taskInfo['response']
      end

      if taskInfo.has_key? 'finishedAt' or taskInfo['terminated']
        break
      end
      sleep 0.8
    end

    if not resp
      raise 'no response'
    end
    resp
  end

  def get_content(resp)
    if resp.has_key? 'compile'
      compile = resp['compile']
      compile_stderr = compile['stderr'].strip
      compile_stdout = compile['stdout'].strip
      if compile['code'] != 0
        raise compile_stderr
      end
    end

    if resp.has_key? 'error'
      raise resp['error']['message']
    end

    if not resp.has_key? 'run'
      return ''
    end

    (resp['run']['stderr'] + resp['run']['stdout']).strip
  end

  def filter(content, channel)
    max_lines = 4
    max_chars = 250
    endc = "\u2026"
    # limit based on line numbers first:
    lines = content.split("\n")
    if lines.length > max_lines
      content = lines[0...max_lines].join("\n") + endc
    end

    # on length:
    if content.length > max_chars
      content = content[0...max_chars] + endc
    end

    # mask nicknames to prevent highlight spam
    if channel.kind_of? Irc::Channel
      nicks = channel.users.map(&:nick)
      nicks.each do |nick|
        masked = nick.gsub(/^(.)/, "\\1\u200B")
        content.gsub! nick, masked
      end
    end

    content
  end

end

plugin = RccvmPlugin.new

