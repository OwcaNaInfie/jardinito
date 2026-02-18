const fs = require('fs');
const path = require('path');

const avatarsPath = path.join(__dirname, '../public/avatars');

// Czytane raz przy starcie aplikacji
let DEFAULT_AVATARS = [];

try {
  DEFAULT_AVATARS = fs
    .readdirSync(avatarsPath)
    .filter(file =>
      file.endsWith('.png') ||
      file.endsWith('.jpg') ||
      file.endsWith('.jpeg')
    );

  if (DEFAULT_AVATARS.length === 0) {
    console.warn('⚠ No default avatars found!');
  }

} catch (err) {
  console.error('❌ Error reading avatars folder:', err);
}

function getRandomDefaultAvatar() {
  if (DEFAULT_AVATARS.length === 0) {
    return null;
  }

  const randomIndex = Math.floor(Math.random() * DEFAULT_AVATARS.length);
  return DEFAULT_AVATARS[randomIndex];
}

module.exports = {
  getRandomDefaultAvatar
};
