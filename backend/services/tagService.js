const tagRepository = require('../repositories/tagRepository');
const sessionRepository = require('../repositories/sessionRepository');

exports.getTags = async (userId) => {
    const userTags = await tagRepository.findByUserId(userId);
    return userTags?.tags || [];
};

exports.createTag = async (userId, name, color) => {
    const userTags = await tagRepository.addTag(userId, { name, color });
    return userTags.tags[userTags.tags.length - 1];
};

exports.reorderTags = async (userId, tagIds) => {
    const userTags = await tagRepository.findByUserId(userId);
    if (!userTags) return null;

    const reordered = tagIds
        .map(id => userTags.tags.find(t => t._id.toString() === id))
        .filter(Boolean);

    console.log('Reordered count:', reordered.length, 'Original count:', userTags.tags.length);
    return tagRepository.saveReordered(userTags, reordered);
};

exports.updateTag = async (userId, tagId, name, color) => {
    const userTags = await tagRepository.updateTag(userId, tagId, { name, color });
    if (!userTags) return null;
    // propagacja zmian do snapshotów sesji -- logika domenowa, nie HTTP
    await sessionRepository.updateTagSnapshot(userId, tagId, { name, color });
    return userTags.tags.find(t => t._id.toString() === tagId);
};

exports.deleteTag = async (userId, tagId) => {
    const result = await tagRepository.removeTag(userId, tagId);
    return result || null;
};