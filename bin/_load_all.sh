#!/bin/bash

if [ $# -eq 0 ]; then
    echo "missing process date(%Y/%m/%d)"
    exit 1
fi

process_date=$1

date "+%Y-%m-%d %H:%M:%S"
set -xe

./_load.sh $process_date 	active_users_android.csv	407996818	
./_load.sh $process_date 	active_users_ios.csv	408287033	
./_load.sh $process_date 	category_auto_parts_android.csv	408295670	
./_load.sh $process_date 	category_auto_parts_ios.csv	408295673	
./_load.sh $process_date 	category_baby___kids_android.csv	408295574	
./_load.sh $process_date 	category_baby___kids_ios.csv	408295577	
./_load.sh $process_date 	category_bags___handbags_android.csv	409632099	
./_load.sh $process_date 	category_bags___handbags_ios.csv	409632207	
./_load.sh $process_date 	category_cars_android.csv	409601100	
./_load.sh $process_date 	category_cars_ios.csv	408019987	
./_load.sh $process_date 	category_cell_phones_android.csv	408020131	
./_load.sh $process_date 	category_cell_phones_ios.csv	408020059	
./_load.sh $process_date 	category_collectibles___art_android.csv	409632243	
./_load.sh $process_date 	category_collectibles___art_ios.csv	408020215	
./_load.sh $process_date 	category_electronics___computers_android.csv	408295616	
./_load.sh $process_date 	category_electronics___computers_ios.csv	407997010	
./_load.sh $process_date 	category_furniture_android.csv	408020065	
./_load.sh $process_date 	category_furniture_ios.csv	408020068	
./_load.sh $process_date 	category_household___tools_android.csv	408020053	
./_load.sh $process_date 	category_household___tools_ios.csv	409632174	
./_load.sh $process_date 	category_jewelry___watches_android.csv	409632198	
./_load.sh $process_date 	category_jewelry___watches_ios.csv	409632201	
./_load.sh $process_date 	category_men_s_clothing___shoes_android.csv	408020170	
./_load.sh $process_date 	category_men_s_clothing___shoes_ios.csv	408020155	
./_load.sh $process_date 	category_sports___outdoors_android.csv	408295667	
./_load.sh $process_date 	category_sports___outdoors_ios.csv	408020167	
./_load.sh $process_date 	category_video_games_android.csv	408020236	
./_load.sh $process_date 	category_video_games_ios.csv	408020221	
./_load.sh $process_date 	category_women_s_clothing___shoes_android.csv	408295625	
./_load.sh $process_date 	category_women_s_clothing___shoes_ios.csv	408020101	
./_load.sh $process_date 	like_users_android.csv	408019981	
./_load.sh $process_date 	like_users_ios.csv	408295382	
./_load.sh $process_date 	offer_users_android.csv	409632063	
./_load.sh $process_date 	offer_users_ios.csv	408295463	
./_load.sh $process_date 	post_or_edit_item_users_android.csv	409632144	
./_load.sh $process_date 	post_or_edit_item_users_ios.csv	409632045	
./_load.sh $process_date 	register_android.csv	408288332	
./_load.sh $process_date 	register_ios.csv	408295418	
./_load.sh $process_date 	no_active_users_ios_7.csv	467359815
./_load.sh $process_date 	no_active_users_android_7.csv	464470493
./_load.sh $process_date 	no_active_users_ios_14.csv	464305144
./_load.sh $process_date 	no_active_users_android_14.csv	464470502
./_load.sh $process_date 	no_active_users_ios_30.csv	464305129
./_load.sh $process_date 	no_active_users_android_30.csv	464470505
./_load.sh $process_date 	no_active_users_ios_60.csv	467359728
./_load.sh $process_date 	no_active_users_android_60.csv	467359791
./_load.sh $process_date 	segment_android_view_category_car.csv 469608434 no_skip
./_load.sh $process_date 	segment_ios_view_category_car.csv 469738885 no_skip
./_load.sh $process_date 	segment_android_search_keywords_car.csv 469738810 no_skip
./_load.sh $process_date 	segment_ios_search_keywords_car.csv 469738957 no_skip
./_load.sh $process_date 	segment_android_view_item_car.csv 472620027 no_skip
./_load.sh $process_date 	segment_ios_view_item_car.csv 469608599 no_skip

set +x
date "+%Y-%m-%d %H:%M:%S"
echo 'done'
