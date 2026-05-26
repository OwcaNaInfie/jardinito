const mongoose = require('mongoose');
require('dotenv').config();
const Plant = require('../models/Plant');

mongoose.connect(process.env.MONGO_URI).then(async () => {
    await seedPlants();
    mongoose.disconnect();
});

const plants = [
    {
        name: 'tulip', nameKey: 'plant_tulip_name', descriptionKey: 'plant_tulip_desc',
        colors: ['RED', 'PINK'], size: 'small',
        minDuration: 30, minDurationDev: 5, price: 0
    },
    {
        name: 'poppy', nameKey: 'plant_poppy_name', descriptionKey: 'plant_poppy_desc',
        colors: ['RED', 'ORANGE'], size: 'medium',
        minDuration: 60, minDurationDev: 10, price: 0
    },
    {
        name: 'sunflower', nameKey: 'plant_sunflower_name', descriptionKey: 'plant_sunflower_desc',
        colors: ['YELLOW', 'ORANGE'], size: 'large',
        minDuration: 90, minDurationDev: 15, price: 0
    },
    {
        name: 'daffodil', nameKey: 'plant_daffodil_name', descriptionKey: 'plant_daffodil_desc',
        colors: ['YELLOW', 'WHITE'], size: 'small',
        minDuration: 30, minDurationDev: 5, price: 200
    },
    {
        name: 'rose', nameKey: 'plant_rose_name', descriptionKey: 'plant_rose_desc',
        colors: ['RED', 'PINK'], size: 'medium',
        minDuration: 60, minDurationDev: 10, price: 260
    },
    {
        name: 'bloodroot', nameKey: 'plant_bloodroot_name', descriptionKey: 'plant_bloodroot_desc',
        colors: ['WHITE', 'YELLOW'], size: 'small',
        minDuration: 30, minDurationDev: 5, price: 320
    },
    {
        name: 'gerbera', nameKey: 'plant_gerbera_name', descriptionKey: 'plant_gerbera_desc',
        colors: ['ORANGE', 'YELLOW'], size: 'medium',
        minDuration: 60, minDurationDev: 10, price: 440
    },
    {
        name: 'pansy', nameKey: 'plant_pansy_name', descriptionKey: 'plant_pansy_desc',
        colors: ['PURPLE', 'BLUE'], size: 'small',
        minDuration: 30, minDurationDev: 5, price: 500
    },
    {
        name: 'lavender', nameKey: 'plant_lavender_name', descriptionKey: 'plant_lavender_desc',
        colors: ['PURPLE', 'BLUE'], size: 'large',
        minDuration: 90, minDurationDev: 15, price: 560
    },
    {
        name: 'lily', nameKey: 'plant_lily_name', descriptionKey: 'plant_lily_desc',
        colors: ['WHITE', 'PINK'], size: 'large',
        minDuration: 90, minDurationDev: 15, price: 680
    },
    {
        name: 'orchid', nameKey: 'plant_orchid_name', descriptionKey: 'plant_orchid_desc',
        colors: ['PINK', 'PURPLE'], size: 'large',
        minDuration: 90, minDurationDev: 15, price: 780
    },
].map(plant => ({
    ...plant,
    images: {
        small: `${plant.name}_s.svg`,
        medium: `${plant.name}_m.svg`,
        mediumOutlined: `${plant.name}_o_m.svg`,
        large: `${plant.name}_l.svg`
    },
    witheredImages: {
        small: 'withered_s.svg',
        medium: 'withered_m.svg',
        mediumOutlined: 'withered_o_m.svg'
    }
}));

const seedPlants = async () => {
    const count = await Plant.countDocuments();
    if (count === 0) {
        await Plant.insertMany(plants);
        console.log('[SEED] Plants seeded successfully');
    }
};

module.exports = { seedPlants };