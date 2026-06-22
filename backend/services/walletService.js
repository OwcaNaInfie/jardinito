const walletRepository = require('../repositories/walletRepository');
const plantRepository = require('../repositories/plantRepository');

exports.getWallet = async (userId) => {
    const wallet = await walletRepository.findByUserId(userId);
    if (!wallet) return null;
    return {
        coins: wallet.coins,
        unlockedPlantIds: wallet.unlockedPlantIds.map(id => id.toString()),
        favouritePlantIds: wallet.favouritePlantIds.map(id => id.toString())
    };
};

exports.buyPlant = async (userId, plantId) => {
    const plant = await plantRepository.findById(plantId);
    if (!plant) throw new Error('PLANT_NOT_FOUND');

    const wallet = await walletRepository.findByUserId(userId);
    if (!wallet) throw new Error('WALLET_NOT_FOUND');

    if (wallet.unlockedPlantIds.map(id => id.toString()).includes(plantId))
        throw new Error('PLANT_ALREADY_UNLOCKED');

    if (wallet.coins < plant.price)
        throw new Error('INSUFFICIENT_COINS');

    wallet.coins -= plant.price;
    wallet.unlockedPlantIds.push(plantId);
    await walletRepository.save(wallet);

    return {
            coins: wallet.coins,
            unlockedPlantIds: wallet.unlockedPlantIds.map(id => id.toString()),
            favouritePlantIds: wallet.favouritePlantIds.map(id => id.toString()) // ← dodaj
        };
};

exports.toggleFavourite = async (userId, plantId) => {
    const wallet = await walletRepository.findByUserId(userId);
    if (!wallet) throw new Error('WALLET_NOT_FOUND');

    const index = wallet.favouritePlantIds.findIndex(id => id.toString() === plantId);
    if (index === -1) {
        wallet.favouritePlantIds.push(plantId);
    } else {
        wallet.favouritePlantIds.splice(index, 1);
    }

    await walletRepository.save(wallet);

    return {
        coins: wallet.coins,
        unlockedPlantIds: wallet.unlockedPlantIds.map(id => id.toString()),
        favouritePlantIds: wallet.favouritePlantIds.map(id => id.toString())
    };
};