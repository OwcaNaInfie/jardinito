const express = require('express');
const router = express.Router();
const tagController = require('../controllers/tagController');

router.get('/', tagController.getTags);
router.post('/', tagController.createTag);
router.put('/reorder', tagController.reorderTags);
router.put('/:tagId', tagController.updateTag);
router.delete('/:tagId', tagController.deleteTag);

module.exports = router;