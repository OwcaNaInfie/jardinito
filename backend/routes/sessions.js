const express = require('express');
const router = express.Router();
const sessionController = require('../controllers/sessionController');

router.post('/', sessionController.createSession);
router.get('/', sessionController.getSessions);
router.get('/:userId', sessionController.getSessionsByPeriod);
router.patch('/:sessionId', sessionController.updateSessionTag);

module.exports = router;