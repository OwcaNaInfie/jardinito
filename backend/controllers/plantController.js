const plantService = require('../services/plantService');

exports.getAllPlants = async (req, res) => {
    try {
        const plants = await plantService.getAllPlants();
        res.json({ plants });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.getPlantById = async (req, res) => {
    try {
        const plant = await plantService.getPlantById(req.params.plantId);
        if (!plant) return res.status(404).json({ message: 'Plant not found' });
        res.json({ plant });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};