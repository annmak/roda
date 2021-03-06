#!/bin/bash

set -x

# install dependencies
curl https://bintray.com/user/downloadSubjectPublicKey?username=bintray | sudo apt-key add -
echo "deb http://dl.bintray.com/siegfried/debian wheezy main" | sudo tee -a /etc/apt/sources.list
sudo apt-get -qq update
sudo apt-get -qq install siegfried -y
sudo sf -update
sudo apt-get -qq install clamav clamav-daemon -y

# Enable cache of clamav databases to improve build time
sudo mv /usr/bin/clamscan /usr/bin/clamscan-real
sudo cp .travis/clamscan /usr/bin/clamscan
sudo chmod a+rx /usr/bin/clamscan
sudo mkdir -p "$CLAMAV_DATABASE"
sudo chown clamav:clamav "$CLAMAV_DATABASE"
sudo freshclam --datadir=$CLAMAV_DATABASE
sudo -u clamav chmod -R a+r "$CLAMAV_DATABASE"

# decrypt maven setting.xml
if [[ ! -z "$encrypted_a8a9ca6bf122_key" ]]; then
  openssl aes-256-cbc -K $encrypted_a8a9ca6bf122_key -iv $encrypted_a8a9ca6bf122_iv -in .travis/settings.xml.enc -out settings.xml -d
fi
