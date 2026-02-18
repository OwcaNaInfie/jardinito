function getRandomDefaultAvatar() {
    const randomIndex = Math.floor(Math.random() * DEFAULT_AVATARS.length);
    return DEFAULT_AVATARS[randomIndex];