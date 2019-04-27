read -p "Commit bundle.js or style.css? (Y/n)  " commitBundle
commitBundle=${commitBundle:-Y}

if [[ $commitBundle = [Yy] ]]; then
  echo "Commiting files"
  git update-index --no-assume-unchanged src/main/resources/static/bundle.js
  git update-index --no-assume-unchanged src/main/resources/static/style.css
elif [[ $commitBundle = [Nn] ]]; then
  echo "Commiting anything else"
  git update-index --assume-unchanged src/main/resources/static/bundle.js
  git update-index --assume-unchanged src/main/resources/static/style.css
else
  echo "I don't understand. Bye!"
  exit 1
fi

if [[ `git status --porcelain` ]]; then
  git add -A
  echo "Enter commit comment"
  read comment
  git commit -m "$comment"
  git push
else
  echo "Nothing to commit!"
fi
