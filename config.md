git config --global user.name "Freddy"
git config --global user.email "baptistaalfredo81@gmail.com"

ssh-keygen -t ed25519 -C "baptistaalfredo81@gmail.com"

cd ~/labs
git init
git remote add origin git@github.com:alfredobaptista/labs-dev.git
git add .
git commit -m "primeiro commit do labs"
git push -u origin main
