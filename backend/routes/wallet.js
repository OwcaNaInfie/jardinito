const express = require('express');
const router = express.Router();
const walletController = require('../controllers/walletController');

router.get('/', walletController.getWallet);
router.post('/buy', walletController.buyPlant);
router.post('/favourite', walletController.toggleFavourite);

module.exports = router;