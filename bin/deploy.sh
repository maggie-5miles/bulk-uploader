#!/bin/bash
rsync -azv target ads-staging:/fivemiles/scripts/bulk-uploader/
rsync -azv bin/*.* ads-staging:/fivemiles/scripts/bulk-uploader/
