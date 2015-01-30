import unittest, os, os.path, sys

sys.path.append(os.path.join(os.path.abspath(os.path.dirname(__file__)), '..'))

import backend

backend.load()

class GCCBackendTest(unittest.TestCase):

    def setUp(self):
        self.runner = backend.Runner()

    def test_compile(self):
        res = self.runner.delegate({
            'backend': 'gcc',
            'files': {
                'main.c': {'data': """
                    #include <stdio.h>
                    int main(void) {
                      printf("Hello World");
                      return 0;
                    }
                """}}})
        self.assertEquals(res['run']['stdout'], 'Hello World')

class Python2BackendTest(unittest.TestCase):

    def setUp(self):
        self.runner = backend.Runner()

    def test_compile(self):
        res = self.runner.delegate({
            'backend': 'python2',
            'files': {
                'main.py': {'data': "print 'Hello World'"}}})
        self.assertEquals(res['run']['stdout'], 'Hello World\n')

class Python3BackendTest(unittest.TestCase):

    def setUp(self):
        self.runner = backend.Runner()

    def test_compile(self):
        res = self.runner.delegate({
            'backend': 'python3',
            'files': {
                'main.py': {'data': "print('Hello World')"}}})
        self.assertEquals(res['run']['stdout'], 'Hello World\n')

class HaskellBackendTest(unittest.TestCase):

    def setUp(self):
        self.runner = backend.Runner()

    def test_compile(self):
        res = self.runner.delegate({
            'backend': 'haskell',
            'files': {
                'main.hs': {'data': 'main = print "Hello World"\n'}}})
        self.assertEquals(res['run']['stdout'], '"Hello World"\n')

class RubyBackendTest(unittest.TestCase):

    def setUp(self):
        self.runner = backend.Runner()

    def test_compile(self):
        res = self.runner.delegate({
            'backend': 'ruby',
            'files': {
                'main.rb': {'data': "puts 'Hello World'"}}})
        self.assertEquals(res['run']['stdout'], 'Hello World\n')

class ScalaBackendTest(unittest.TestCase):

    def setUp(self):
        self.runner = backend.Runner()

    def test_compile(self):
        res = self.runner.delegate({
            'backend': 'scala',
            'files': {
                'main.sc': {'data': 'println("Hello World")'}}})
        self.assertEquals(res['run']['stdout'], 'Hello World\n')

class TclBackendTest(unittest.TestCase):

    def setUp(self):
        self.runner = backend.Runner()

    def test_compile(self):
        res = self.runner.delegate({
            'backend': 'tcl',
            'files': {
                'main.tcl': {'data': 'puts "Hello World"'}}})
        self.assertEquals(res['run']['stdout'], 'Hello World\n')

class JavaBackendTest(unittest.TestCase):

    def setUp(self):
        self.runner = backend.Runner()

    def test_compile(self):
        res = self.runner.delegate({
            'backend': 'java',
            'files': {
                'Main.java': {'data': """
                    class Main {
                      public static void main(String[] args) {
                        System.out.println("Hello World");
                      }
                    }
                """}}})
        self.assertEquals(res['run']['stdout'], 'Hello World\n')

class PerlBackendTest(unittest.TestCase):

    def setUp(self):
        self.runner = backend.Runner()

    def test_compile(self):
        res = self.runner.delegate({
            'backend': 'perl',
            'files': {
                'main.pl': {'data': 'print "Hello World"'}}})
        self.assertEquals(res['run']['stdout'], 'Hello World')

if __name__ == '__main__':
    unittest.main()

