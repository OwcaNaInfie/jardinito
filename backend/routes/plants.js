const express = require('express');
const router = express.Router();
const Plant = require('../models/Plant');

// GET
router.get('/', async (req, res) => {
    try {
        const plants = await Plant.find();
        res.json({ plants });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

// GET single plant
router.get('/:plantId', async (req, res) => {
    try {
        const plant = await Plant.findById(req.params.plantId);
        if (!plant) return res.status(404).json({ message: 'Plant not found' });
        res.json({ plant });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

module.exports = router;