# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure(2) do |config|
  config.vm.box = "12wrigja/LabyrinthServer"

  config.vm.network "forwarded_port", guest: 4567, host: 4567
  config.vm.network "forwarded_port", guest: 5005, host: 5005
  config.vm.network "private_network", ip: "192.168.60.10"

  config.vm.synced_folder ".", "/home/vagrant/LabyrinthServer"
  config.vm.synced_folder "../HappyTweet/.", "/home/vagrant/HappyTweet"
  
  config.vm.provider "virtualbox" do |v|
    v.memory=2048
  end
end
