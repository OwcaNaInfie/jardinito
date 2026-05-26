const mongoose = require('mongoose');
require('dotenv').config();
const Plant = require('../models/Plant');

mongoose.connect(process.env.MONGO_URI).then(async () => {
    await seedPlants();
    mongoose.disconnect();
});

const plants = [
    // Podstawowe (price: 0)
    { name: "tulip",     minDuration: 30, minDurationDev: 5, price: 0   },
    { name: "poppy",     minDuration: 60, minDurationDev: 10, price: 0   },
    { name: "sunflower", minDuration: 90, minDurationDev: 15, price: 0   },

    // 3-6 sesji
    { name: "daffodil",  minDuration: 30, minDurationDev: 5, price: 200 },
    { name: "rose",      minDuration: 60, minDurationDev: 10, price: 260 },
    { name: "bloodroot", minDuration: 30, minDurationDev: 5, price: 320 },

    // 7-10 sesji
    { name: "gerbera",   minDuration: 60, minDurationDev: 10, price: 440 },
    { name: "pansy",     minDuration: 30, minDurationDev: 5, price: 500 },
    { name: "lavender",  minDuration: 90, minDurationDev: 15, price: 560 },

    // 11-13 sesji
    { name: "lily",      minDuration: 90, minDurationDev: 15, price: 680 },
    { name: "orchid",    minDuration: 90, minDurationDev: 15, price: 780 },
].map(plant => ({
    ...plant,
    images: {
        small: `${plant.name}_s.svg`,
        medium: `${plant.name}_m.svg`,
        mediumOutlined: `${plant.name}_o_m.svg`,
        large: `${plant.name}_l.svg`
    },
    witheredImages: {
        small: "withered_s.svg",
        medium: "withered_m.svg",
        mediumOutlined: "withered_o_m.svg"
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