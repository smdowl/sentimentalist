import json
import logging
from os.path import join
import sys
from tempfile import NamedTemporaryFile

from boto.s3.connection import S3Connection
from boto.s3.key import Key


logging.basicConfig(level=logging.INFO)


class CompanySplitter(object):

    def __init__(self, input, output):
        self.input = input
        self.output = output
        self.company_files = {}

        conn = S3Connection(aws_access_key_id="AKIAIQQS32WOARUW24EA",
                            aws_secret_access_key="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")

        self.bucket = conn.get_bucket('sentimentalist')

    def split_files(self):
        source_keys = self.bucket.list(prefix=self.input)

        for key in source_keys:
            temp = NamedTemporaryFile()
            logging.info('Downloading ' + key.key)
            key.get_contents_to_filename(temp.name)

            logging.info('Downloaded. Starting processing')
            self.process_file(temp)
            logging.info('Processed.')

        logging.info('Storing output')    
        self.store_output()
        logging.info('Done!')

    def process_file(self, file):
        for line in file.readlines():
            time, json_str = line.split('\t')
            obj = json.loads(json_str)

            self.store_object(time, obj)

    def store_object(self, time, obj):
        company = self.get_company(obj)

        if company is None:
            return

        if company not in self.company_files:
            self.company_files[company] = NamedTemporaryFile()

        self.company_files[company].write(str(time) + '\t' + json.dumps(obj) + '\n')

    def get_company(self, obj):

        company = None

        for key in obj.keys():
            if key.startswith("company:"):
                company = key.split("company:")[1]

        return company

    def store_output(self):

        for company in self.company_files:
            out_path = join(self.output, company)

            f = self.company_files[company]
            f.flush()

            key = Key(self.bucket)
            key.key = out_path

            logging.info('Uploading %s' % company)
            key.set_contents_from_filename(f.name)


if __name__ == "__main__":
    input = sys.argv[1]
    output = sys.argv[2]

    splitter = CompanySplitter(input, output)
    splitter.split_files()

