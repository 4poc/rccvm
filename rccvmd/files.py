import os
from os.path import dirname, join
import shutil
import uuid
import logging

logger = logging.getLogger('rccvm')

class RequestFiles(object):
    def __init__(self, files):
        self.files = files
        self.path = join('/tmp', str(uuid.uuid4()))

    def store(self):
        for filename, contents in self.files.iteritems():
            filename = join(self.path, filename)
            logger.info('store file: '+filename)
            os.makedirs(dirname(filename))
            with open(filename, 'w') as f:
                f.write(contents['data'])
                logger.info('store in file data: '+contents['data'])

    def destroy(self):
        logger.info('destroy stored request files: ' + self.path)
        shutil.rmtree(self.path)

