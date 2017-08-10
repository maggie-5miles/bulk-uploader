#!/bin/bash
rsync -azv target ads-console-1:/fivemiles/scripts/bulk-uploader/
rsync -azv bin/*.* ads-console-1:/fivemiles/scripts/bulk-uploader/
